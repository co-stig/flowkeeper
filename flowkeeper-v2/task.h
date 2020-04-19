#ifndef TASK_H
#define TASK_H

#include "backlog.h"
#include "workitem.h"

#include <QObject>

class Task : public QObject, public Workitem
{
    Q_OBJECT

public:
    explicit Task(Backlog *parent, QString title, bool planned);

private:
    QString title;
    bool planned;

signals:

};

#endif // TASK_H
