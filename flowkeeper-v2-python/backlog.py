from abstract_strategy import AbstractStrategy

class Backlog:
    def __init__(self, name, date):
        self._name = name
        self._date = date
        self._items = dict()

    def __getitem__(self, key):
        return self._items[key]

    def __contains__(self, key):
        return key in self._items

    def __setitem__(self, key, value):
        self._items[key] = value

    def __delitem__(self, key):
        del self._items[key]

    def __str__(self):
        res = f'Backlog "{self._name}" for {self._date}:'
        for item in self._items.values():
            res += f'\n{item}'
        return res


class Backlogs:
    def __init__(self):
        self._global = Backlog("__all__", "2000-01-01")
        self._items = {"__all__": self._global}

    def __getitem__(self, key):
        return self._items[key]

    def __contains__(self, key):
        return key in self._items

    def __setitem__(self, key, value):
        self._items[key] = value

    def __delitem__(self, key):
        del self._items[key]

    def __str__(self):
        return "\n".join([str(b) for b in self._items.values()])

    def get_global(self):
        return self._global


BACKLOGS = Backlogs()


# CreateBacklog("The first backlog", "2020-04-19")
class CreateBacklogStrategy(AbstractStrategy):
    def __init__(self, name, params):
        super().__init__(name, params)
        self._backlog_name = params[0]
        self._backlog_date = params[1]

    def execute(self):
        if self._backlog_name in BACKLOGS:
            raise Exception(f'Backlog "{self._backlog_name}" already exists')
        backlog = Backlog(self._backlog_name, self._backlog_date)
        BACKLOGS[self._backlog_name] = backlog


# AddToBacklog("The first backlog", "Eat breakfast")
class AddToBacklogStrategy(AbstractStrategy):
    def __init__(self, name, params):
        super().__init__(name, params)
        self._backlog_name = params[0]
        self._workitem_name = params[1]

    def execute(self):
        if self._backlog_name not in BACKLOGS:
            raise Exception(f'Backlog "{self._backlog_name}" not found')
        if self._workitem_name not in BACKLOGS.get_global():
            raise Exception(f'Workitem "{self._workitem_name}" not found')
        backlog = BACKLOGS[self._backlog_name]
        if self._workitem_name in backlog:
            raise Exception(f'Workitem "{self._workitem_name}" is already in backlog "{self._backlog_name}"')
        backlog[self._workitem_name] = BACKLOGS.get_global()[self._workitem_name]
