FROM java:8

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS="" \
    TZ=Asia/Shanghai

# add directly the war
ADD *.war /app.war

EXPOSE 10168
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    echo "${TZ}" > /etc/timezone && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war
