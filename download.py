from subprocess import call

url = 'http://aminer.org/lab-datasets/citation/DBLP-citation-Jan8.tar.bz2'
save_name = 'dblp-v4.tar.bz2'

cmds = [
    'wget {} -O {}'.format(url, save_name),
    'tar -jxvf {}'.format(save_name),
    'mv DBLP-citation-Jan8.txt dblp-v4.txt'
    'rm {}'.format(save_name)
]

call(cmds, cwd='./src/main/resources', shell=True)
