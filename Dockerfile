FROM openjdk:17
COPY ./target /tmp
WORKDIR /tmp
ENTRYPOINT ["/tmp/classes/uk/ac/ed/inf/","App"]
# Please note that the THIRD-PARTY-LICENSE could be out of date if the base image has been updated recently.
# The Corretto team will update this file but you may see a few days' delay.