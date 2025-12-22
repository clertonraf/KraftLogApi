#!/bin/bash
print_info() { echo -e "\033[0;32m[INFO]\033[0m $1"; }

CURRENT=$(grep -oP '<version>\K[^<]+' pom.xml | head -1)
print_info "Current: $CURRENT"

echo "New version:"
read NEW

sed -i.bak "s/<version>$CURRENT<\/version>/<version>$NEW<\/version>/" pom.xml && rm pom.xml.bak
git add pom.xml
git commit -m "Bump version to $NEW"
git tag -a "v$NEW" -m "Release $NEW"
git push origin main && git push origin "v$NEW"

print_info "Done! Monitor: https://github.com/clertonraf/KraftLogApi/actions"
