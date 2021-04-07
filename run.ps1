<# TODO: turn these into proper parameters #>

$JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk15.0.2_7"
$GRAPHVIZ_HOME = "C:\Program Files (x86)\Graphviz2.38"
$TRANSFORMER_PATH = "nyct-pathways-transformer-cli/target/nyct-pathways-transformer-cli-1.0-SNAPSHOT-withAllDependencies.jar"
$INPUT_PATH = "Data\Input"
$OUTPUT_PATH = "Data\Output"
$GTFS_OUTPUT_PATH = Join-Path -Path $OUTPUT_PATH -ChildPath "pathways_out"
$GTFS_AGENCY_ID = "MTA_NYCT"

$transforms = @(
@{
    "op" = "transform";
    "class" = "com.nyct.dos.tpc.pathways.NyctPathwaysTransformStrategy";
    "base_path" = Join-Path -Path $INPUT_PATH -ChildPath "Pathways Export";
    "platform_stop_mapping_file" = Join-Path -Path $INPUT_PATH -ChildPath "GTFS - Platform Stop IDs - Final.csv";
    "stations_file" = Join-Path -Path $INPUT_PATH -ChildPath "Stations.csv";
    "station_complexes_file" = Join-Path -Path $INPUT_PATH -ChildPath "StationComplexes.csv";
    "equipment_file" = Join-Path -Path $INPUT_PATH -ChildPath "allequipments.xml"
},

@{ "op" = "remove"; "match" = @{ "file" = "routes.txt"; "agency_id" = $GTFS_AGENCY_ID } },
@{ "op" = "remove"; "match" = @{ "file" = "calendar.txt"; "service_id" = "m/.*/" } },
@{ "op" = "remove"; "match" = @{ "file" = "calendar_dates.txt"; "service_id" = "m/.*/" } }
)

$transforms_json = $transforms | ForEach-Object { $_ | ConvertTo-JSON -Compress }

$transforms_file = New-TemporaryFile

$transforms_json -Join [System.Environment]::Newline | Out-File $transforms_file -Encoding ascii -NoNewline

& $(Join-Path -Path $JAVA_HOME -ChildPath "bin\java") -jar $TRANSFORMER_PATH `
    --transform="$($transforms_file)" `
    --agencyId "$($GTFS_AGENCY_ID)" `
    $(Join-Path -Path $INPUT_PATH -ChildPath "GTFS_RAPID_20200429_REV202004291056240.zip") `
    $($GTFS_OUTPUT_PATH)

if ($?)
{
    $Stations = Import-Csv -Path $(Join-Path -Path $INPUT_PATH -ChildPath "Stations.csv")

    $DIAGRAMS_BASE = Join-Path -Path $OUTPUT_PATH -ChildPath "diagrams"

    Import-Csv -Path $(Join-Path -Path $GTFS_OUTPUT_PATH -ChildPath "stops.txt" ) `
  | Where-Object stop_id -Match "^MR[0-9]{3}$" `
  | ForEach-Object { $_.stop_id } `
  | ForEach-Object {
        $dot_file = Join-Path -Path $DIAGRAMS_BASE -ChildPath "$( $_ ).dot"
        $png_file = Join-Path -Path $DIAGRAMS_BASE -ChildPath "$( $_ ).png"

        $stop_ids = (,$_ + ($Stations `
                         | Where-Object "Complex ID" -EQ $( [int]($_ -Replace "^MR", "") ) `
                         | ForEach-Object { $_."GTFS Stop ID" })) -Join ","

        & python visualize_pathways.py --stop_ids $stop_ids -d $dot_file $($GTFS_OUTPUT_PATH)
        & $(Join-Path -Path $GRAPHVIZ_HOME -ChildPath "bin\neato") -Gstart=self -Goverlap=false -Gsplines=true -o $png_file -Tpng $dot_file
    }

    Compress-Archive -DestinationPath $(Join-Path -Path $OUTPUT_PATH -ChildPath "diagrams.zip") -Path $(Join-Path -Path $DIAGRAMS_BASE -ChildPath "*.png")
    Compress-Archive -DestinationPath $(Join-Path -Path $OUTPUT_PATH -ChildPath "google_transit_pathways.zip") -Path $(Join-Path -Path $GTFS_OUTPUT_PATH -ChildPath "*")
}

Remove-Item $transforms_file -Force
