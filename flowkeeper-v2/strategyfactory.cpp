#include "strategycreatebacklog.h"
#include "strategyfactory.h"
#include <iostream>

StrategyFactory::StrategyFactory(): paramRegex("([a-zA-Z]+)\\s*\\(\\s*\"\\s*((?:[^\"\\\\]|\\\\.)*)\\s*\"\\s*(?:,\\s*\"\\s*((?:[^\"\\\\]|\\\\.)*)\\s*\"\s*)(?:,\\s*\"\\s*((?:[^\"\\\\]|\\\\.)*)\\s*\"\\s*)*\\)")
{
    //backlogStrategies.insert("CreateBacklog", StrategyCreateBacklog());
}

void StrategyFactory::execute(QString str)
{
    QRegularExpressionMatch m = paramRegex.match(str);
    if (!m.hasMatch())
    {
        std::cout << "Invalid strategy syntax: " << str.toStdString() << std::endl;
        throw "Invalid strategy syntax";
    }

    std::cout << "Func: " << m.captured(1).toStdString() << std::endl;
    std::cout << "1 param: " << m.captured(2).toStdString() << std::endl;
    std::cout << "2 param: " << m.captured(3).toStdString() << std::endl;
    std::cout << "3 param: " << m.captured(4).toStdString() << std::endl;
}
