#ifndef BACKLOG_H
#define BACKLOG_H

#include "enums.h"
#include "user.h"
#include "workitem.h"

#include <QObject>

class Backlog : public QObject, public Workitem
{
    Q_OBJECT

public:
    explicit Backlog(User *parent, QString title, QDate forDate);
    explicit Backlog(User *parent, QString title);

private:
    QString title;
    QDate forDate;
    bool daily;

signals:

};

#endif // BACKLOG_H
