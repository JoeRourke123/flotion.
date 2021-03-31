from datetime import datetime
from os import environ
from urllib.parse import quote, unquote
from flask import Flask, render_template
from notion.client import NotionClient
from notion.collection import CollectionView, Collection, CollectionRowBlock

from utils.consts import CARD_PAGE
from utils.filter_builder import build_filter
from utils.modify_cards import change_cover
from utils.card_details import get_cover_level, get_card_module, get_card_content, get_card_title, \
    get_modules_from_collection, pick_random_card, count_results, get_module_property_id, get_correct_property_id
from utils.statistics_thread import StatisticsThread

NotionClient.pick_random_card = pick_random_card
NotionClient.count_results = count_results

app = Flask(__name__)
app.secret_key = environ.get("FLOTION_TOKEN", None)
notion = NotionClient(
    token_v2=environ.get("FLOTION_TOKEN", None),
    enable_caching=True
)

cards_db: CollectionView = notion.get_collection_view(CARD_PAGE)
statistics = {}

fetcher = StatisticsThread(notion, cards_db, statistics)
fetcher.start()


@app.route('/')
def main():
    return render_template('index.html')


@app.route('/questions')
def questions():
    return render_template('quiz.html')


@app.route('/stats')
def stats_view():
    return render_template('statistics.html')


@app.route("/c/<path:url>", methods=["POST"])
def correct(url):
    try:
        item = notion.get_block(unquote(url))
    except Exception as e:
        print(e)
        return {"error": True}

    item.answered = datetime.now()
    got_correct = item.correct
    item.correct = got_correct + 1

    change_cover(item, correct=got_correct + 1)

    return {"success": True}


@app.route('/modules')
def modules():
    db = notion.get_collection_view(CARD_PAGE)
    coll: Collection = db.collection

    return {"modules": get_modules_from_collection(coll)}


@app.route('/q/<card_filter>')
def question(card_filter: str):
    populated = False
    parsed_card = {"error": True}

    split_card_filter = card_filter.split("__")
    module_filter = None if split_card_filter[0] == "all" else split_card_filter[0]
    understanding_filter = None if len(split_card_filter) < 2 else split_card_filter[1].lower()

    module_id = get_module_property_id(cards_db.collection)
    correct_id = get_correct_property_id(cards_db.collection)
    filter_object = build_filter(
        module_filter,
        understanding_filter,
        module_id=module_id,
        correct_id=correct_id
    )

    while not populated:
        random_item: CollectionRowBlock = notion.pick_random_card(cards_db, query=filter_object)

        if random_item is None:
            break

        parsed_card = {
            "id": quote(random_item.get_browseable_url()),
            "title": get_card_title(random_item),
            "content": get_card_content(random_item),
            "module": get_card_module(random_item),
            "level": get_cover_level(random_item)
        }
        populated = len(parsed_card["content"]) > 0

    return parsed_card


@app.route("/s")
def stats_data():
    return statistics


if __name__ == '__main__':
    app.run(host='0.0.0.0')
