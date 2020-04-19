#include "backlog.h"

Backlog::Backlog(User *parent, QString title, QDate forDate) : QObject(parent)
{
    this->title = title;
    this->forDate = forDate;
    this->daily = true;
}

Backlog::Backlog(User *parent, QString title) : QObject(parent)
{
    this->title = title;
    this->daily = false;
}
