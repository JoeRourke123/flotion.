def get_stats(rows: list, modules: list) -> dict:
    module_mapping = {m: [0, 0, 0] for m in modules}
    module_cards = {m: 0 for m in modules}

    response = {
        "total_red": 0,
        "total_yellow": 0,
        "total_green": 0,
        "total_corrects": 0,
        "total_cards": 0,
    }

    for item in rows:
        cover = get_cover_level(item)
        response[f"total_{cover}"] += 1
        response["total_corrects"] += 0 if item.correct is None else item.correct
        response["total_cards"] += 1

        cover_index = ["red", "green", "yellow"].index(cover)
        if item.module is not None:
            module_mapping[item.module][cover_index] += 1
            module_cards[item.module] += 1

        time.sleep(0.1)

    response["module_understanding"] = module_mapping

    for module in modules:
        card_count = module_cards[module]

        if card_count == 0:
            card_count = 1

        module_mapping[module][0] /= card_count
        module_mapping[module][1] /= card_count
        module_mapping[module][2] /= card_count

    response["ranked_modules"] = sorted(modules, key=lambda x: module_mapping[x])

    return response