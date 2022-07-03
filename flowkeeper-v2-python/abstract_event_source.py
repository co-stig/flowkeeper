class AbstractEventSource:

    def __init__(self):
        pass

    def replay(self, since=None):
        pass

    def push(self, strategies):
        pass


