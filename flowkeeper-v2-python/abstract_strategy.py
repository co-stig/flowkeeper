class AbstractStrategy:
    def __init__(self, name, params):
        self._name = name
        self._params = params

    def __str__(self):
        # Escape params
        params = '", "'.join([p.replace('"', '\\"') for p in self._params])
        if len(params) > 0:
            params = '"' + params + '"'
        return f'{self._name}({params})'

    def execute(self):
        pass


class EmptyStrategy(AbstractStrategy):
    def __init__(self, string_as_is):
        self._string_as_is = string_as_is.strip()

    def __str__(self):
        return self._string_as_is

    def execute(self):
        pass
