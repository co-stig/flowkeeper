#include "filestrategysource.h"
#include "strategyfactory.h"

#include <QFile>
#include <QTextStream>

FileStrategySource::FileStrategySource(QString filename)
{
    StrategyFactory factory;

    QFile in(filename);
    if (in.open(QIODevice::ReadOnly))
    {
        QTextStream stream(&in);
        while (!stream.atEnd())
        {
            QString line = in.readLine();
            if (!line.trimmed().isEmpty()) {
                factory.execute(line);
            }
        }
        in.close();
    }
}
