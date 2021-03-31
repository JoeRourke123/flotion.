import os
import sqlite3

from utils.consts import DIRECTORY


def parse_html():
    conn = sqlite3.connect('flotion.db')

    for filename in os.listdir(DIRECTORY):
        if filename.endswith("html"):
