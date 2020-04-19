#ifndef STRATEGYFACTORY_H
#define STRATEGYFACTORY_H

#include "strategy.h"
#include "user.h"

#include <QMap>
#include <QObject>
#include <QRegularExpression>


class StrategyFactory
{
public:
    StrategyFactory();
    void execute(QString str);

private:
    QMap<QString, Strategy<User>> backlogStrategies;
    QRegularExpression paramRegex;
};

#endif // STRATEGYFACTORY_H
