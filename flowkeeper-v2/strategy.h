#ifndef STRATEGY_H
#define STRATEGY_H

#include <QStringList>



template <class T>
class Strategy
{
public:
    Strategy();
    virtual void process(T& context, QStringList args) = 0;
};

#endif // STRATEGY_H
