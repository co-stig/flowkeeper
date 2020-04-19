#ifndef WORKITEM_H
#define WORKITEM_H

#include "enums.h"

#include <QDateTime>

class Workitem
{
public:
    Workitem();

protected:
    QDateTime start;
    QDateTime end;
    State state;
};

#endif // WORKITEM_H
