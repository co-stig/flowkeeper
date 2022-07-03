from PySide6.QtCore import QAbstractTableModel

class BacklogModel(QAbstractTableModel):
    def __init__(self, parent, backlog):
        super().__init__(parent)
        self.backlog = backlog

    def rowCount(self, parent):
        return len(self.backlog._items)

    def columnCount(self, parent):
        return 4

    def data(self, index, role):
        workitem = list(self.backlog._items.values())[index.row()]
        col = index.column()
        if col == 0:
            return workitem._state
        elif col == 1:
            return workitem._name
        elif col == 2:
            return ",".join(workitem._tags)
        elif col == 3:
            return "".join([str(p) for p in workitem._pomodoros])
        else:
            raise Exception(f'Unexpected column: {col}')
