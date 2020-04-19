#include "task.h"

Task::Task(Backlog *parent, QString title, bool planned) : QObject(parent)
{
    this->title = title;
    this->planned = planned;
}
