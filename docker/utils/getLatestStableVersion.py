#!/usr/bin/env python2

import urllib2
import xml.etree.ElementTree as ET


class JenkinsVersion():
    metadataUrl = 'https://repo.jenkins-ci.org/releases/org/jenkins-ci/main/jenkins-war/maven-metadata.xml'

    def getStable(self):
        try:
            stable_version = []
            url = self.metadataUrl
            tree = ET.parse(urllib2.urlopen(url))
            root = tree.getroot()

            versions = root.findall('versioning/versions/version')

            for version in versions:
                if len(version.text.split('.')) == 3:
                    stable_version.append(version.text)

            return stable_version[-1]

        except urllib2.URLError, e:
            print 'Something went wrong while retrieving stable version: {}'.format(e)


if __name__ == "__main__":
    print JenkinsVersion().getStable()
