overlap = open("./processing/overlap.tsv", "r")

#####################
#
# pre-processing
#
#####################

ontos = []
matrix = []
size = {}
final = {}
dcr = {} # direct cross-references
scr = {} # shared cross-references
oldst = {} # overlapping logical definitions s>t
oldts = {} # overlapping logical definitions t>s
sld = {} # shared logical definitions
rc = {} # reused classes
rop = {} # reused object properties
rdp = {} # reused data properties
lm = {} # lexical matcher
wm = {} # word matcher
for line in overlap:
    column = line.strip().split("\t")
    if column[0] != "pairs":
        y = column[0].split("_")
        pair = y[1].split("/")
        o1 = pair[0].strip(",").replace("-","")
        o2 = pair[1].strip(",").replace("-", "")
        oo1 = o1.split(".")
        oo2 = o2.split(".")
        pair = oo1[0] + "/" + oo2[0]
        pair2 = oo2[0] + "/" + oo1[0]
        
        if oo1[0] not in ontos:
            ontos.append(oo1[0])
        if oo2[0] not in ontos:
            ontos.append(oo2[0])
        
        sourceClasses = column[11]
        targetClasses = column[12]
        
        if sourceClasses < targetClasses:
            size[pair] = sourceClasses
            size[pair2] = sourceClasses 
        else:
            size[pair] = targetClasses
            size[pair2] = targetClasses
        
        if column[1] != "direct cross-refs":
            dcr[pair] = int(column[1])
            dcr[pair2] = int(column[1])
        if column[2] != "shared cross-refs":
            scr[pair] = int(column[2])
            scr[pair2] = int(column[2])
        if column[3] != "overlapping log defs s>t":
            oldst[pair] = int(column[3])
            oldst[pair2] = int(column[3])
        if column[4] != "overlapping log defs t>s":
            oldts[pair] = int(column[4])
            oldts[pair2] = int(column[4])
        if column[5] != "shared log defs":
            sld[pair] = int(column[5])
            sld[pair2] = int(column[5])
        if column[6] != "reused classes":
            rc[pair] = int(column[6])
            rc[pair2] = int(column[6])
        if column[7] != "reused object properties":
            rop[pair] = int(column[7])
            rop[pair2] = int(column[7])
        if column[8] != "reused data properties":
            rdp[pair] = int(column[8])
            rdp[pair2] = int(column[8])
        if column[9] != "LM":
            lm[pair] = int(column[9])
            lm[pair2] = int(column[9])

            
df = pd.DataFrame()
df.index = ontos

for o1 in ontos:
    i = ontos.index(o1)
    list = []
    for o2 in ontos:
        pair = o1 + "/" + o2
        j = ontos.index(o2)
        if o1 != o2:
            if pair in dcr:
                a = dcr[pair]
                b = scr[pair]
                c = oldst[pair]
                d = oldts[pair]
                e = sld[pair]
                f = rc[pair]
                g = rop[pair]
                h = rdp[pair]
                k = lm[pair]
                s = size[pair]
                sum = a + b + c + d + e + f + g + h + k
                if sum != 0:
                    v = (sum)/float(s)
                    list.append(v)
                    df.loc[o1,o2] = v
                    final[pair] = v
                else:
                    list.append(0)
                    df.loc[o1,o2] = 0
                    final[pair] = 0
            else:
                list.append(0)
                df.loc[o1,o2] = 0
                final[pair] = 0 
        else:
            list.append(1)
            df.loc[o1,o2] = 1
            final[pair] = 1 
    matrix.append(list)
                  
df.to_csv('./processing/dataframes/matrix.csv')

#####################
#
# clustering
#
#####################

sc = SpectralClustering(n_clusters=4, affinity='precomputed', assign_labels='kmeans')
sc_clustering = sc.fit(matrix)
# print(sc_clustering.labels_)

cluster = {}
j = 0
for i in sc_clustering.labels_:
    o = ontos[j]
    cluster[o] = i
    j = j + 1

l0 = []
l1 = []
l2 = []
l3 = []
clusters = {}
for o in cluster:
    i = cluster[o]
    if i == 0:
        l0.append(o)
    elif i == 1:
        l1.append(o)
    elif i == 2:
        l2.append(o)
    elif i == 3:
        l3.append(o)
clusters[0] = l0
clusters[1] = l1
clusters[2] = l2
clusters[3] = l3
print(clusters)    

x = matrix[:][0]
y = matrix[:][1]

fig, ax = plt.subplots()
sns.scatterplot(x=x, y=y, data=df.assign(cluster = sc_clustering.labels_), hue='cluster', ax=ax, palette='colorblind')
