#ifndef USER_H
#define USER_H

#include <QObject>

class User : public QObject
{
    Q_OBJECT

public:
    explicit User(QString name, QString email);

private:
    QString name;
    QString email;

signals:

};

#endif // USER_H
