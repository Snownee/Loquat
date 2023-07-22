import os
import re
import sys

# get `mod_version` from gradle.properties
path = os.getcwd()
file = open(os.path.join(path, 'gradle.properties'), 'r')
text = file.read()
pattern = re.compile(r'mod_version\s?=\s?(.*)')
mod_version = pattern.findall(text)[0]
parts = mod_version.split('.')
assert len(parts) == 3, 'mod_version must be in format major.minor.patch'

# increment `mod_version` based on publish_type
if len(sys.argv) == 1:
    publish_type = 'patch'
else:
    publish_type = sys.argv[1]
publish_type = ['major', 'minor', 'patch'].index(publish_type)
assert publish_type >= 0, 'publish_type must be major, minor or patch'
parts[publish_type] = str(int(parts[publish_type]) + 1)
for i in range(publish_type + 1, 3):
    parts[i] = '0'
mod_version = '.'.join(parts)
print('Publishing version: ' + mod_version)

# write new `mod_version` to gradle.properties
file.close()
file = open(os.path.join(path, 'gradle.properties'), 'w')
text = pattern.sub('mod_version=' + mod_version, text)
file.write(text)
file.close()

# get current branch name
branch = os.popen('git rev-parse --abbrev-ref HEAD').read().strip()
platform = branch.split('-')[1]
tag = platform + '-' + mod_version

# push a new tag to git
os.system('git add gradle.properties')
os.system('git commit -m "Publish version ' + mod_version + '"')
os.system('git tag ' + tag)
os.system('git push')
os.system('git push --tags')

# generate changelog from git commits
recent_tags = os.popen('''git for-each-ref refs/tags --sort=creatordate --format='%(refname:lstrip=2)' --count=6 --merged''').read().split('\n')
if (len(recent_tags) > 1):
    change_log = os.popen('git log ' + recent_tags[0].replace("'", '') + '..HEAD --pretty=format:"%s"').readlines()
    with open('CHANGELOG.md', 'w') as f:
        first_line = True
        is_empty = False
        for line in change_log:
            if line.startswith('Publish version'):
                if is_empty:
                    f.write('- No changelog provided\n')
                if not first_line:
                    f.write('\n')
                f.write(line.replace('Publish version', '##') + '\n')
                is_empty = True
            else:
                f.write('- ' + line)
            first_line = False
else:
    with open('CHANGELOG.md', 'w') as f:
        f.write('')

# run gradle task to publish mod
os.system('gradlew publishUnified')
