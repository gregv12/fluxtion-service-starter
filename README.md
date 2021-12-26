[![MavenCI](https://github.com/gregv12/fluxtion-service-starter/actions/workflows/main.yml/badge.svg)](https://github.com/gregv12/fluxtion-service-starter/actions/workflows/main.yml)
[![Release to maven central](https://github.com/gregv12/fluxtion-service-starter/actions/workflows/release.yml/badge.svg)](https://github.com/gregv12/fluxtion-service-starter/actions/workflows/release.yml)
# Fluxtion service starter
A server for managing the deterministic execution of start and stop tasks for a set of interdependent services. Implemented
with [Fluxtion](https://github.com/v12technology/fluxtion) to manage the underlying directed acyclic graph of services.

Documentation available on **[GitHub pages](https://gregv12.github.io/fluxtion-service-starter/)** 

### Overview
In many systems services execute independently but need to co-ordinate their lifecycle with each other. A service
may require all downstream services to be started before starting and becoming available to accept upstream requests.

Similarly, if a downstream service becomes unavailable all upstream services will need to be notified and take appropriate 
actions. 

As systems grow a complex graph of interdependent services quickly arises, the difficulty in correctly managing 
lifecycle overwhelms a handwritten manual solution. Service starter provides an automated utility for managing the lifecycle of independent services, executing
start and stop tasks associated with a particular service at the correct time.

