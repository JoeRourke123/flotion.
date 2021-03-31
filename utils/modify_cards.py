from notion.collection import CollectionRowBlock

from utils.consts import *


def change_cover(item: CollectionRowBlock, correct=None):
    if correct is None:
        corrects = item.correct
    else:
        corrects = correct

    cover = item.cover

    limits = [GREEN_LIMIT, YELLOW_LIMIT, 0]
    covers = ["/images/page-cover/solid_blue.png",
              "/images/page-cover/solid_yellow.png",
              "/images/page-cover/solid_red.png"]
    for i in range(len(limits)):
        if corrects >= limits[i] and cover != covers[i]:
            item.cover = covers[i]
            break