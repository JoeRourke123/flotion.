from utils.consts import GREEN_LIMIT, YELLOW_LIMIT


def build_filter(module: str, understanding: str, module_id="module", correct_id="correct") -> dict:
    filters = []

    if module is not None:
        filters.append({
            "filter": {
                "operator": "enum_is",
                "value": {
                    "type": "exact",
                    "value": module
                }
            },
            "property": module_id
        })
    if understanding is not None:
        lower_limit = {
            "yellow": YELLOW_LIMIT, "green": GREEN_LIMIT
        }.get(understanding, -1)
        upper_limit = {
            "red": YELLOW_LIMIT, "yellow": GREEN_LIMIT
        }.get(understanding, -1)

        if lower_limit >= 0:
            filters.append({
                "filter": {
                    "operator": "number_greater_than_or_equal_to",
                    "value": {
                        "type": "exact",
                        "value": lower_limit
                    }
                },
                "property": correct_id
            })
        if upper_limit >= 0:
            filters.append({
                "filter": {
                    "operator": "number_less_than",
                    "value": {
                        "type": "exact",
                        "value": upper_limit
                    }
                },
                "property": correct_id
            })

    return {
        "filter": {
            "filters": filters,
            "operator": "and"
        }
    }
