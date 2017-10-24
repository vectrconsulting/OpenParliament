echo '{"name":"'$1'","entries":[' > $1.json

curl -H accept:application/json -H content-type:application/json -d '{"statements":[{"statement":"MATCH (a:'$1') RETURN a.name_nl"}]}' http://localhost:1024/db/data/transaction/commit |
    sed 's/{"row"/\n/g'  |
    sed 's/:\["\(.*\)"\],"meta".*/\1/g' |
    tail -n +2 |
    sed 's/\(.*\)/{"value":"\1", "synonyms": ["\1", "\L\1"]},/g' |
    sed '$ s/.$//' >> $1.json

echo ']}' >> $1.json
