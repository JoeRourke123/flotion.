from utils.consts import YELLOW_LIMIT, GREEN_LIMIT


def filter_cards(cards, card_filter: str):
    split_filter = card_filter.split("__")
    parsed_module = split_filter[0]
    parsed_difficulty = None if len(split_filter) < 2 else split_filter[1].lower()

    lower_difficulty_bound = {
        "yellow": YELLOW_LIMIT, "green": GREEN_LIMIT
    }.get(parsed_difficulty, -1)
    upper_difficulty_bound = {
        "red": YELLOW_LIMIT - 1, "yellow": GREEN_LIMIT - 1
    }.get(parsed_difficulty, -1)

    if parsed_module == "all" and lower_difficulty_bound < 0 and upper_difficulty_bound < 0:
        return cards
    else:
        return list(filter(lambda x: (parsed_module == "all" or x.module == parsed_module) and
                                     (parsed_difficulty is None or (
                                         lower_difficulty_bound < 0 or x.correct >= lower_difficulty_bound
                                     ) and (
                                         upper_difficulty_bound < 0 or x.correct <= upper_difficulty_bound
                                     )), cards))
