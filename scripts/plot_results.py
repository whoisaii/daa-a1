"""Generate SVG plots from real CSV data; requires reportlab (pip install reportlab)."""
import csv
import math
from pathlib import Path
from reportlab.graphics.shapes import Drawing, String, Line, Rect
from reportlab.graphics.charts.lineplots import LinePlot
from reportlab.graphics import renderSVG
from reportlab.lib import colors

ROOT = Path(__file__).resolve().parents[1]
rows = list(csv.DictReader((ROOT / 'results/results.csv').open()))
names = ['MergeSort', 'QuickSort', 'DeterministicSelect', 'ClosestPair']
palette = ['#2563eb', '#e76f24', '#009e73', '#9b51bd']
for metric, title, filename in [('median_time_ns', 'Execution time vs. input size', 'time-vs-n.svg'), ('max_depth', 'Maximum recursion depth vs. input size', 'depth-vs-n.svg')]:
    d = Drawing(1120, 820)
    d.add(Rect(0, 0, 1120, 820, fillColor=colors.white, strokeColor=None))
    d.add(String(36, 782, title, fontName='Helvetica-Bold', fontSize=23, fillColor=colors.HexColor('#162237')))
    d.add(String(36, 757, 'Seed 20260927 | 5 warmups + 7 measured trials | Java 17 | n = 100 to 100,000', fontSize=12, fillColor=colors.HexColor('#526174')))
    for i, name in enumerate(names):
        x=38+i*268
        d.add(Line(x,730,x+24,730,strokeColor=colors.HexColor(palette[i]),strokeWidth=3))
        d.add(String(x+30,726,name,fontSize=12))
    for idx, kind in enumerate(['random','sorted','reverse','duplicates']):
        x=78+(idx%2)*546; y=438-(idx//2)*338
        plot=LinePlot(); plot.x=x; plot.y=y; plot.width=440; plot.height=224
        plot.data=[]
        for name in names:
            subset=sorted((r for r in rows if r['algorithm']==name and r['input_type']==kind),key=lambda r:int(r['n']))
            plot.data.append([(math.log10(int(r['n'])),math.log10(int(r[metric])/1e6) if metric=='median_time_ns' else int(r[metric])) for r in subset])
        plot.xValueAxis.valueMin=2; plot.xValueAxis.valueMax=5; plot.xValueAxis.valueStep=1
        plot.xValueAxis.labelTextFormat=lambda v: f'{10**v:,.0f}'
        plot.xValueAxis.labels.fontSize=10
        plot.yValueAxis.labels.fontSize=10
        plot.yValueAxis.visibleGrid=True; plot.yValueAxis.gridStrokeColor=colors.HexColor('#e3e9ef')
        if metric=='median_time_ns':
            plot.yValueAxis.valueMin=-4; plot.yValueAxis.valueMax=2; plot.yValueAxis.valueStep=1
            plot.yValueAxis.labelTextFormat=lambda v: f'{10**v:g}'
        else:
            plot.yValueAxis.valueMin=0; plot.yValueAxis.valueMax=20; plot.yValueAxis.valueStep=5
        for i, color in enumerate(palette):
            plot.lines[i].strokeColor=colors.HexColor(color); plot.lines[i].strokeWidth=2
            from reportlab.graphics.widgets.markers import makeMarker
            plot.lines[i].symbol=makeMarker('FilledCircle'); plot.lines[i].symbol.size=5
            plot.lines[i].symbol.fillColor=colors.HexColor(color)
            plot.lines[i].symbol.strokeColor=colors.HexColor(color)
        d.add(plot)
        d.add(String(x,y+249,kind.title() + (' (8 integer values / 8 x 8 point grid)' if kind=='duplicates' else ''),fontName='Helvetica-Bold',fontSize=13))
        d.add(String(x+190,y-36,'Input size n (log scale)',fontSize=10,textAnchor='middle'))
        d.add(String(x,y+230,'Median time (ms, log scale)' if metric=='median_time_ns' else 'Active algorithm frames',fontSize=10))
    d.add(String(36,28,'Timings include instrumentation and algorithm allocations. Depth is the maximum across 7 measured trials.',fontSize=11,fillColor=colors.HexColor('#526174')))
    renderSVG.drawToFile(d,str(ROOT/'docs/plots'/filename))
    print('Created', filename)
