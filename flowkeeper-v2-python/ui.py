import sys
import random
from PySide6 import QtCore, QtWidgets, QtGui
from file_event_source import FileEventSource
from backlog import BACKLOGS

class MainWindow(QtWidgets.QWidget):
    def __init__(self, backlogs):
        super().__init__()
        self.backlogs = backlogs

        print()
        print('*** Strategies ***')
        print(source)

        print()
        print('*** Data store ***')
        print(backlogs)

        self.layout = QtWidgets.QVBoxLayout(self)

        self.text = QtWidgets.QLabel("Hello World", alignment=QtCore.Qt.AlignCenter)
        self.layout.addWidget(self.text)

        self.add_workitem = QtWidgets.QPushButton("Add workitem")
        self.add_workitem.clicked.connect(self.magic)
        self.layout.addWidget(self.add_workitem)

        self.add_pomodoro = QtWidgets.QPushButton("Add pomodoro")
        self.add_pomodoro.clicked.connect(self.magic)
        self.layout.addWidget(self.add_pomodoro)

    @QtCore.Slot()
    def magic(self):
        self.text.setText(random.choice(['1', '2', '3']))


if __name__ == "__main__":
    source = FileEventSource('test.txt')
    source.replay()

    app = QtWidgets.QApplication([])

    widget = MainWindow(BACKLOGS)
    widget.resize(800, 600)
    widget.show()

    sys.exit(app.exec())
