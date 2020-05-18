# aws-missing-alarm-audit
Does AWS CloudFormation declare a Lambda or EC2 resource without any alarms?

By default it will check Lambda or LoadBalancer

```
sbt "run org"
```

You can provide other resources like so

```
sbt "run org Lambda LoadBalancer Dynamo"
```

or to make it run faster provide GitHub personal access token

```
export token="*********" && sbt "run guardian"
```
