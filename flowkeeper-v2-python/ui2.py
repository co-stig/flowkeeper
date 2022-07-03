from PySide6.QtWidgets import QApplication, QVBoxLayout, QTableView, QHeaderView
from file_event_source import FileEventSource
from backlog import BACKLOGS
from backlog_model import BacklogModel

if __name__ == "__main__":
    source = FileEventSource('test.txt')
    source.replay()
    backlog = BACKLOGS['The first backlog']

    a = QApplication([])
    tableView = QTableView()
    tableView.setModel(BacklogModel(a, backlog))
    tableView.show()
    tableView.resize(800, 600)
    tableView.horizontalHeader().setSectionResizeMode(1, QHeaderView.Stretch)
    a.exec()
