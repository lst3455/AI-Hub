curl -X POST \
http://172.29.32.1:8090/api/v0/chat/completions \
-H 'Content-Type: application/json;charset=utf-8' \
-H 'Authorization: a8b8' \
-d '{
"messages": [
{
"content": "write a bubble sort",
"role": "user"
}
],
"model": "GLM_4"
}'