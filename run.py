#!/usr/bin/env python3
import os
import sys
from subprocess import call
from time import sleep
import pkg_resources

TASK = sys.argv[1]

COMPOSE_FILES = 'stack.yml'
CONTAINER = 'birdeye-auth'
STACK_NAME = 'vossibility'
STACK_PORT = '8080'

MODIFY_STACK_FILE = False

installed_packages = {pkg.key for pkg in pkg_resources.working_set}

if 'python-dotenv' in installed_packages:
    from dotenv import load_dotenv

    load_dotenv()
    NEW_STACK_NAME = os.getenv('STACK_NAME')
    if NEW_STACK_NAME is not None:
        print('New stack name: "{}" found in env'.format(NEW_STACK_NAME))
        STACK_NAME = NEW_STACK_NAME
    NEW_STACK_PORT = os.getenv('STACK_PORT')
    if NEW_STACK_PORT is not None:
        print('New stack port: "{}" found in env'.format(NEW_STACK_PORT))
        STACK_PORT = NEW_STACK_PORT
        MODIFY_STACK_FILE = True
else:
    print('''WARNING: python-dotenv module not found in installed packages. 
    Consider installing it for reading env values related to multiple environment variables.''')


def get_modified_compose_file_path():
    if 'pyyaml' not in installed_packages:
        print('''pyyaml not found in install packages. Install it via:
        
            pip install pyyaml
        ''')
        exit(-1)
    import yaml
    with open(COMPOSE_FILES) as compose_file:
        doc = yaml.load(compose_file, Loader=yaml.FullLoader)
        doc['services']['birdeye-auth']['ports'][0] = '{}:8080'.format(STACK_PORT)
        tmp_file_path = '/tmp/{}-{}-{}.yml'.format(CONTAINER, STACK_NAME, STACK_PORT)
    with open(tmp_file_path, 'w+') as tmp_file:
        yaml.dump(doc, tmp_file, default_flow_style=False, sort_keys=False)
    return tmp_file_path


def get_first_container():
    return ' $(docker ps --filter name=' + STACK_NAME + '_' + CONTAINER + ' --format {{.ID}}' + ' | head -n 1' + ')'


def up():
    # call(['bash', '-c', 'docker stack deploy --compose-file=' + COMPOSE_FILES + ' ' + STACK_NAME])
    if MODIFY_STACK_FILE:
        call(['bash', '-c',
              'cat {} | docker stack deploy --compose-file - {}'.format(get_modified_compose_file_path(), STACK_NAME)])
    else:
        call(['bash', '-c', 'cat {} | docker stack deploy --compose-file - {}'.format(COMPOSE_FILES, STACK_NAME)])


def down():
    call(['bash', '-c', 'docker service rm ' + STACK_NAME + '_' + CONTAINER])


def build():
    call(['bash', '-c', 'docker build -t ' + CONTAINER + ' -f Dockerfile .'])


def log():
    if len(sys.argv) > 2:
        call(['bash', '-c', 'docker logs --follow ' + sys.argv[2]])
    else:
        call(['bash', '-c', 'docker logs --follow ' + get_first_container()])


def bash():
    call(['bash', '-c', 'docker exec -it ' + sys.argv[2] + ' bash'])


def migrate():
    if len(sys.argv) > 2:
        call(['bash', '-c', 'docker exec -it' + sys.argv[2] + ' python manage.py migrate'])
    else:
        call(['bash', '-c', 'docker exec -it' + get_first_container() + ' python manage.py migrate'])


if TASK == 'up':
    up()
elif TASK == 'down':
    down()
elif TASK == 'log':
    log()
elif TASK == 'build':
    build()
elif TASK == 'bash':
    bash()
elif TASK == 'dup':
    down()
    up()
elif TASK == 'bup':
    build()
    down()
    up()
elif TASK == 'migrate':
    migrate()
elif TASK == 'deploy':
    build()
    down()
    up()
    sleep(10)
    migrate()
elif TASK == 'container_name':
    call(['bash', '-c', "echo" + get_first_container()])
elif TASK == 'get_bash':
    call(['bash', '-c', 'docker exec -it ' + get_first_container() + " bash"])
else:
    call(['bash', '-c', 'echo "See ya :)"'])
