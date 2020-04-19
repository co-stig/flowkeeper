#include "backlog.h"
#include "strategycreatebacklog.h"

void StrategyCreateBacklog::process(User& user, QStringList args) {
    switch (args.length()) {
    case 1:
        new Backlog(&user, args[0]);
        break;
    case 2:
        new Backlog(&user, args[0], QDate::fromString(args[1]));
        break;
    default:
        throw "Creating backlog requires one or two arguments: (TITLE, [FOR_DATE])";
    }
}
