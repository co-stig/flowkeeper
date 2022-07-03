from abstract_strategy import AbstractStrategy
from backlog import BACKLOGS


class Pomodoro:
    # State is one of the following: new, running, finished, canceled (AKA void)
    def __init__(self, is_planned, state):
        self._is_planned = is_planned
        self._state = state

    def __str__(self):
        if self._state == 'new':
            char = ' '
        elif self._state == 'running':
            char = '*'
        elif self._state == 'finished':
            char = '✓'
        elif self._state == 'canceled':
            char = 'X'
        else:
            raise Exception(f'Invalid pomodoro state:{self._state}')
        if self._is_planned:
            return f'[{char}]'
        else:
            return f'({char})'

    def seal(self, target_state):
        if target_state in ('finished', 'canceled'):
            self._state = target_state
        else:
            raise Exception(f'Invalid pomodoro state: {target_state}')

    def start(self):
        self._state = 'running'

    def is_running(self):
        return self._state == 'running'

    def is_startable(self):
        return self._state == 'new'


class Workitem:
    def __init__(self, name, tags, num_pomodoros):
        self._name = name
        self._tags = tags
        self._pomodoros = list()
        # State is one of the following: new, running, finished, canceled
        self._state = 'new'
        for i in range(num_pomodoros):
            self._pomodoros.append(Pomodoro(True, 'new'))

    def __getitem__(self, key):
        return self._pomodoros[key]

    def __str__(self):
        if self._state == 'new':
            char = ' '
        elif self._state == 'running':
            char = '*'
        elif self._state == 'finished':
            char = '✓'
        elif self._state == 'canceled':
            char = 'X'
        else:
            raise Exception(f'Invalid workitem state:{self._state}')

        return f' - [{char}] {self._name} ({",".join(self._tags)}) | {"".join([str(p) for p in self._pomodoros])}'

    def seal(self, target_state):
        if target_state in ('finished', 'canceled'):
            self._state = target_state
        else:
            raise Exception(f'Invalid workitem state: {target_state}')

    def is_running(self):
        return self._state == 'running'

    def is_sealed(self):
        return self._state in ('finished', 'canceled')

    def start(self):
        self._state = 'running'


# CreateWorkitem("Wake up", "planned", "3")
class CreateWorkitemStrategy(AbstractStrategy):
    def __init__(self, name, params):
        super().__init__(name, params)
        self._workitem_name = params[0]
        self._workitem_tags = params[1].split(',')
        self._num_pomodoros = int(params[2])

    def execute(self):
        if self._workitem_name in BACKLOGS.get_global():
            raise Exception(f'Workitem "{self._workitem_name}" already exists')
        workitem = Workitem(self._workitem_name, self._workitem_tags, self._num_pomodoros)
        BACKLOGS.get_global()[self._workitem_name] = workitem


# CompleteWorkitem("Wake up", "canceled")
class CompleteWorkitemStrategy(AbstractStrategy):
    def __init__(self, name, params):
        super().__init__(name, params)
        self._workitem_name = params[0]
        self._target_state = params[1]

    def execute(self):
        if self._workitem_name not in BACKLOGS.get_global():
            raise Exception(f'Workitem "{self._workitem_name}" not found')
        workitem = BACKLOGS.get_global()[self._workitem_name]
        workitem.seal(self._target_state)


# StartPomodoro("Wake up")
class StartPomodoroStrategy(AbstractStrategy):
    def __init__(self, name, params):
        super().__init__(name, params)
        self._workitem_name = params[0]

    def execute(self):
        if self._workitem_name not in BACKLOGS.get_global():
            raise Exception(f'Workitem "{self._workitem_name}" not found')
        workitem = BACKLOGS.get_global()[self._workitem_name]

        if workitem.is_sealed():
            raise Exception(f'Cannot start pomodoro on a sealed workitem "{self._workitem_name}"')

        for pomodoro in workitem:
            if pomodoro.is_running():
                raise Exception(f'There is another running pomodoro in "{self._workitem_name}"')

        for pomodoro in workitem:
            if pomodoro.is_startable():
                pomodoro.start()
                workitem.start()
                return

        raise Exception(f'No pomodoros available in "{self._workitem_name}"')



# CompletePomodoro("Wake up", "finished")
class CompletePomodoroStrategy(AbstractStrategy):
    def __init__(self, name, params):
        super().__init__(name, params)
        self._workitem_name = params[0]
        self._target_state = params[1]

    def execute(self):
        if self._workitem_name not in BACKLOGS.get_global():
            raise Exception(f'Workitem "{self._workitem_name}" not found')
        workitem = BACKLOGS.get_global()[self._workitem_name]
        if not workitem.is_running():
            raise Exception(f'Workitem "{self._workitem_name}" is not running')
        for pomodoro in workitem:
            if pomodoro.is_running():
                pomodoro.seal(self._target_state)
                return
        raise Exception(f'No running pomodoros in "{self._workitem_name}"')
