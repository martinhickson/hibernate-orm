#!/usr/bin/env bash
# Rewrite this project's version and published groupId for a fork release.
# Generated Maven POMs then use io.github.martinhickson (or RELEASE_GROUP)
# so the artifacts can be published to Maven Central under that namespace.
set -euo pipefail

VERSION="${1:?release version required, e.g. 6.6.7-bravura-1}"
GROUP="${2:-io.github.martinhickson}"

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${root}"

echo "Rewriting release coordinates: version=${VERSION} group=${GROUP}"

sed -i "s/^hibernateVersion=.*/hibernateVersion=${VERSION}/" gradle/version.properties

# Project group used by Gradle publications (becomes <groupId> in generated POMs).
sed -i "s/^group = .*/group = '${GROUP}'/" gradle/base-information.gradle
sed -i "s/^group = .*/group = '${GROUP}'/" \
	tooling/hibernate-enhance-maven-plugin/hibernate-enhance-maven-plugin.gradle

# Any remaining explicit project group assignments for published modules.
sed -i "s/groupId = 'org.hibernate.orm'/groupId = '${GROUP}'/g" gradle/published-java-module.gradle

# Sample / tutorial POMs that still declare this project's groupId.
while IFS= read -r -d '' pom; do
	sed -i \
		-e "s|<groupId>org.hibernate.orm</groupId>|<groupId>${GROUP}</groupId>|g" \
		-e "s|<groupId>org.hibernate.orm.tooling</groupId>|<groupId>${GROUP}</groupId>|g" \
		"${pom}"
done < <(find . -name pom.xml -not -path '*/.git/*' -not -path '*/target/*' -print0)

echo "gradle/version.properties:"
cat gradle/version.properties
echo
echo "Published group samples:"
grep -n "group = " gradle/base-information.gradle tooling/hibernate-enhance-maven-plugin/hibernate-enhance-maven-plugin.gradle
