#!/usr/bin/env groovy

import jenkins.model.*
import hudson.security.*
import hudson.security.csrf.*
import hudson.model.Item

def instance = Jenkins.instance

def ldap_url = "ldaps://ldap.jenkins.io"
def ldap_dn = "dc=jenkins-ci,dc=org"
def ldap_admin_dn = "cn=admin,dc=jenkins-ci,dc=org"
def ldap_admin_password = ""

def ldap_admin_password = { username ->
    def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
            com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials.class,
            jenkins.model.Jenkins.instance
            )

    def c = creds.findResult { it.username == ldap_admin_dn ? it : null }

    if ( c ) {
        return c.password
    } else {
        println "could not find credential for ${username}"
    }
}

instance.numExecutors = 0

println "Checking CSRF protection..."
if (instance.crumbIssuer == null) {
  println "Enabling CSRF protection"
  instance.crumbIssuer = new DefaultCrumbIssuer(true)
}

println 'Checking authentication status'
if (instance.securityRealm == SecurityRealm.NO_AUTHENTICATION) {
  println "Enabling LDAP-based authentication"

  // public LDAPSecurityRealm(String server, String rootDN, String userSearchBase, String userSearch, String groupSearchBase, String groupSearchFilter, LDAPGroupMembershipStrategy groupMembershipStrategy, String managerDN, Secret managerPasswordSecret, boolean inhibitInferRootDN, boolean disableMailAddressResolver, CacheConfiguration cache, EnvironmentProperty[] environmentProperties, String displayNameAttributeName, String mailAddressAttributeName, IdStrategy userIdStrategy, IdStrategy groupIdStrategy) {

  LDAPSecurityRealm realm  = new LDAPSecurityRealm(
    ldap_url,
    ldap_dn,
    "ou=people", /* hard-coding this to our schema */
    "cn={0}",    /* -- */
    "ou=groups", /* -- */
    "",          /* -- */
    new jenkins.security.plugins.ldap.FromGroupSearchLDAPGroupMembershipStrategy(''),
    ldap_admin_dn,
    new hudson.util.Secret(ldap_admin_password),
    false,
    false,
    new LDAPSecurityRealm.CacheConfiguration(100, 300), /* cache 100 items for 5 minutes */
    null, /* no environmentProperties */
    "displayname", /* hard-coding this to our schema */
    "email",       /* -- */
    IdStrategy.CASE_INSENSITIVE,
    IdStrategy.CASE_INSENSITIVE,
  )

  instance.securityRealm = realm
}

/* Every time we run, it's worth re-defining our authorization strategy to make
 * sure that it is correct
 */
AuthorizationStrategy strategy = new GlobalMatrixAuthorizationStrategy()

[
    Jenkins.READ,
    Item.READ,
].each { permission ->
    strategy.add(permission, 'release') /*We may have to add a new ldap group*/
}

strategy.add(Jenkins.ADMINISTER, 'admins')
/* give our role account superpowers */
strategy.add(Jenkins.ADMINISTER, 'jenkins')

/* https://issues.jenkins-ci.org/browse/INFRA-1346 */
[
    Jenkins.READ,
    Item.BUILD,
    Item.CANCEL,
    Item.CONFIGURE,
    Item.CREATE,
    Item.DELETE,
    Item.DISCOVER,
    Item.READ,
].each { permission ->
    strategy.add(permission, 'jenkins-admins')
}
instance.authorizationStrategy = strategy

instance.save()
