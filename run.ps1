& java -jar nyct-pathways-transformer-cli/target/nyct-pathways-transformer-cli-1.0-SNAPSHOT-withAllDependencies.jar `
    --transform=transform.json `
    --agencyId "MTA NYCT" `
    GTFS_RAPID_20200429_REV202004291056240.zip `
    pathways_out

if ($?)
{
    $Stations = Import-Csv -Path Stations.csv

    Import-Csv -Path pathways_out/stops.txt `
  | Where-Object stop_id -Match "^MR[0-9]{3}$" `
  | ForEach-Object { $_.stop_id } `
  | ForEach-Object {
        $dot_file = "diagrams/$( $_ ).dot"
        $png_file = "diagrams/$( $_ ).png"

        $stop_ids = (,$_ + ($Stations `
                         | Where-Object "Complex ID" -EQ $( [int]($_ -Replace "^MR", "") ) `
                         | ForEach-Object { $_."GTFS Stop ID" })) -Join ","

        & python visualize_pathways.py --stop_ids $stop_ids -d $dot_file pathways_out
        & "C:\Program Files (x86)\Graphviz2.38\bin\neato" -Gstart=self -Goverlap=false -Gsplines=true -o $png_file -Tpng $dot_file
    }
}

