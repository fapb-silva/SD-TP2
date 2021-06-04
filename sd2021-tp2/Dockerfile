# base image
FROM nunopreguica/sd2021tpbase

# working directory inside docker image
WORKDIR /home/sd

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd2021.jar

# copy the file of properties to the docker image
COPY trab.props trab.props

# copy keystore users
COPY userserver.ks userserver.ks

# copy keystore spreadsheets
COPY sheetserver.ks sheetserver.ks

# copy truststore
COPY truststore.ks truststore.ks

# run Discovery when starting the docker image
CMD ["java", "-cp", "/home/sd/sd2021.jar", \
"sd2021.aula2.server.UserServer"]