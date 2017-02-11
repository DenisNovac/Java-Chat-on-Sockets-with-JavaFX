ENCODING OF THIS AND SOURCE FILES IS UTF-8
COMMENTS IN CYRILLIC
Версия c GUI, сервер в отдельной папке без GUI!



MyChat - чат, основанный на сокетах на Java.
Данная версия включает в себя модуль MVC, позволивший реализовать графический пользовательский интерфейс посредством преобразования всех служебных сообщений в возвращаемые данные.




Команда для сборки в каталоге bin (выполняется из корня):
javac -d bin/ -cp src/ src/mainPck/MainClass.java

Запуск из каталога bin:
java mainPck.MainClass

Запуск jar, если не находит main класс:
java -cp gui.jar mainPck.Main
