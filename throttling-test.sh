#!/usr/bin/env bash

cust_func(){
  ./proxy-curl -v http://facebook.com 2>&1 | grep 'HTTP/1.1 '
}
# For loop 5 times
for i in {1..300}
do
	echo $(cust_func $i) & # Put a function in the background
done
 
## Put all cust_func in the background and bash 
## would wait until those are completed 
## before displaying all done message
wait 
echo "All done"