import time
from threading import Thread

from notion.client import NotionClient
from notion.collection import CollectionView

from utils.card_details import get_modules_from_collection, get_module_property_id, get_correct_property_id
from utils.filter_builder import build_filter


class StatisticsThread(Thread):
    """
    Every 5 minutes, refreshes the cards results.
    """
    def __init__(self, client, collection, statistics):
        super().__init__()

        self.client = client
        self.collection = collection
        self.statistics = statistics

    def run(self):
        while True:
            print("Building statistics.")
            self.collection.refresh()

            stats = get_statistics(self.client, self.collection)
            for k, v in stats.items():
                self.statistics[k] = v

            print("Waiting for 3 minutes.")
            time.sleep(60 * 3)


def get_statistics(client: NotionClient, db: CollectionView):
    modules = get_modules_from_collection(db.collection)

    stats = {
        "total_red": 0,
        "total_yellow": 0,
        "total_green": 0,
        "total_cards": 0,
        "module_understandings": [[0,0,0] for m in modules],
        "module_totals": [],
        "modules": modules
    }

    red_cards_count = []
    yellow_cards_count = []
    green_cards_count = []

    module_prop_id = get_module_property_id(db.collection)
    correct_prop_id = get_correct_property_id(db.collection)

    module_order_map = {}

    for x, module in enumerate(modules):
        for understanding in ["red", "yellow", "green"]:
            current_filter = build_filter(module, understanding, module_prop_id, correct_prop_id)

            locals()[understanding + "_cards_count"].append(
                client.count_results(db, query=current_filter)
            )

        stats["module_understandings"][x] = [
            red_cards_count[x], yellow_cards_count[x], green_cards_count[x]
        ]

        module_total_cards = sum(stats["module_understandings"][x])

        stats["total_red"] += red_cards_count[x]
        stats["total_yellow"] += yellow_cards_count[x]
        stats["total_green"] += green_cards_count[x]
        stats["total_cards"] += module_total_cards
        stats["module_totals"].append(module_total_cards)

        module_order_map[module] = [i / module_total_cards for i in stats["module_understandings"][x]]

    return stats
