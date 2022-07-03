from abstract_event_source import AbstractEventSource
from strategy_factory import strategy_from_string


class FileEventSource(AbstractEventSource):
    def __init__(self, filename):
        super().__init__()
        self._filename = filename
        self._log = list()

    def replay(self):
        with open(self._filename) as f:
            for line in f:
                s = strategy_from_string(line)
                s.execute()
                self._log.append(s)

    def push(self, strategies):
        with open(self._filename, 'a') as f:
            for s in strategies:
                f.write(str(s) + '\n')
                self._log.append(s)

    def __str__(self):
        return '\n'.join([str(s) for s in self._log])
