---
title: Terminology
permalink: lang_terminology.html
---

## DSL Script Code

Support below is your code that sends mail as per the details given.

```groovy
Email.send {
    
    smtpserver {
    
      host     'host'
      port     'port'
      username 'myname'
      password 'mypass'
    }
    
    to 'mailid@domain.com'
    
    cc 'copy@domain.com'
    
    body '''
    Hello world,
    
    Gilli is a very famous game among the youngsters in Tamil Nadu (
    a State in Southern Part of India)
    
    The word Gilli is also the word in Tamil Language that is
    colloquially used to specify a great achievement or great achiever
    in Tamil Nadu.
    
    Happy Gilli !
    
    Regards,
    
    Gilli team
    '''
}
```


This code uses the Domain Specific Language for sending Email. Without thinking too much, any script writer can do this easy coding and it will send an email when executed. This code is called **DSL Script code**. i.e., the code that uses any domain specific language to achieve its end objectives is called **DSL Script code**

The people who write the DSL Script code need not be real developers. Any person who wants to automate his/her routine tasks can write DSL Script code. These people need not know the host programming language in which the respective DSL Infra code is written. Even though, the syntax of the **DSL script** is governed by the host programming language, these people need not understand the concepts, philosophies of the host programming language. However, knowing that language will definitely help these **DSL Script Code**rs write better scripts to achieve their end objectives.

## DSL Infra Code

In the same example given above, that DSL might have been implemented using any of the host programming languages (in our case, it is groovy). That implementation would contain the codes what the keywords **smtpserver**, **to**, **cc**, **body** should do. Also, establishing the connectivity with the smtpserver and finally submits the email content.

Therefore, the code that creates the keywords for particular domain along with its implementations is called **DSL Infra code**.

DSL Infra code is usually written by developers who know the host programming languages.
