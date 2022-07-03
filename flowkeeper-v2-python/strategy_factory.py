import re
from backlog import CreateBacklogStrategy, AddToBacklogStrategy
from workitem import CreateWorkitemStrategy, StartPomodoroStrategy, CompletePomodoroStrategy, CompleteWorkitemStrategy
from abstract_strategy import EmptyStrategy

REGEX = re.compile(r'([a-zA-Z]+)\s*\(\s*"\s*((?:[^"\\]|\\.)*)\s*"\s*(?:,\s*"\s*((?:[^"\\]|\\.)*)\s*"s*)(?:,\s*"\s*((?:[^"\\]|\\.)*)\s*"\s*)*\)')


def strategy_from_string(s):
    # Empty strings and comments are special cases
    if s.strip() == '' or s.startswith('#'):
        return EmptyStrategy(s)

    m = REGEX.search(s)
    if m is not None:
        name = m.group(1)
        params = list(filter(lambda p: p is not None, m.groups()[1:]))
        if name == 'CreateBacklog':
            return CreateBacklogStrategy(name, params)
        elif name == 'CreateWorkitem':
            return CreateWorkitemStrategy(name, params)
        elif name == 'AddToBacklog':
            return AddToBacklogStrategy(name, params)
        elif name == 'CompleteWorkitem':
            return CompleteWorkitemStrategy(name, params)
        elif name == 'CompletePomodoro':
            return CompletePomodoroStrategy(name, params)
        elif name == 'StartPomodoro':
            return StartPomodoroStrategy(name, params)
        else:
            raise Exception(f"Unknown strategy: {name}")
    else:
        raise Exception(f"Bad syntax: {s}")

