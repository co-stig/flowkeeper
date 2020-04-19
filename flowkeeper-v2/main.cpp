#include "backlog.h"
#include "filestrategysource.h"
#include "pomodoro.h"
#include "strategyfactory.h"
#include "task.h"

#include <QGuiApplication>
#include <QQmlApplicationEngine>

Backlog* prepareSampleBacklog() {
    User* user = new User("John Doe", "jd@example.com");
    Backlog* backlog = new Backlog(user, "Backlog");
    Task* task1 = new Task(backlog, "Develop data model", true);
    new Pomodoro(task1);
    new Pomodoro(task1);
    new Pomodoro(task1);
    return backlog;
}

int main(int argc, char *argv[])
{
    FileStrategySource file("flowkeeper/test.txt");
    Backlog* backlog = prepareSampleBacklog();
    StrategyFactory sf;

    QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);

    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;
    const QUrl url(QStringLiteral("qrc:/main.qml"));
    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
                     &app, [url](QObject *obj, const QUrl &objUrl) {
        if (!obj && url == objUrl)
            QCoreApplication::exit(-1);
    }, Qt::QueuedConnection);
    engine.load(url);

    return app.exec();
}
