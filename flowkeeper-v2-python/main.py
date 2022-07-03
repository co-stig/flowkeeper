import configparser
import json
import re
import xmpp
from file_event_source import FileEventSource
from backlog import BACKLOGS

class Server:
    def __init__(self):
        config = configparser.ConfigParser()
        config.read('settings.ini')
        settings = config['Settings']
        self.email = settings['Email']
        self.password = settings['Password']
        jid = xmpp.protocol.JID(self.email)
        self.connection = xmpp.Client(server=jid.getDomain(), debug=(settings['Debug'] == 'True'))
        self.connection.connect()
        self.connection.auth(user=jid.getNode(), password=self.password, resource=jid.getResource())

    def send(self, receiver, message):
        self.connection.send(xmpp.protocol.Message(to=receiver, body=message))


if __name__ == '__main__':
    #server = Server()
    #server.send(receiver="setwindowtext@xmpp.jp", message="Hello, world!")
    source = FileEventSource('test.txt')
    source.replay()

    print()
    print('*** Strategies ***')
    print(source)

    print()
    print('*** Data store ***')
    print(BACKLOGS)
