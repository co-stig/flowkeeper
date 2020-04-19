#include "user.h"

User::User(QString name, QString email) : QObject(nullptr)
{
    this->name = name;
    this->email = email;
}
