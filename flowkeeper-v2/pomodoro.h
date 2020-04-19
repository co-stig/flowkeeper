#ifndef POMODORO_H
#define POMODORO_H

#include "task.h"
#include "workitem.h"

#include <QObject>
#include <qdatetime.h>

class Pomodoro : public QObject, public Workitem
{
    Q_OBJECT

public:
    explicit Pomodoro(Task *parent);

private:
    bool planned;

signals:

};

#endif // POMODORO_H
