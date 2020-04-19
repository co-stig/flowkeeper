#ifndef STRATEGYCREATEBACKLOG_H
#define STRATEGYCREATEBACKLOG_H

#include "strategy.h"
#include "user.h"



class StrategyCreateBacklog: public Strategy<User>
{
public:
    virtual void process(User& context, QStringList args);
};

#endif // STRATEGYCREATEBACKLOG_H
