from subprocess import call

url = 'https://static.aminer.org/lab-datasets/citation/dblp.v9.zip'
fname = url.split('/')[-1]

cmds = [
    'wget {}'.format(url),
    'unzip {}'.format(fname),
    'rm {}'.format(fname)
]

call(cmds, cwd='./src/main/resources', shell=True)
