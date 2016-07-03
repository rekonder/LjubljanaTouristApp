#include "operations.h"

static const int N_EXAMPLES = 7;

struct distancePoint pointDistance;

vector<int> sumArr;

vector<distancePoint> buildingsDist;



/*
    Function which help to compare our structures at sort function
*/
bool cmd(const distancePoint & p1, const distancePoint & p2) {
    return p1.dist < p2.dist;
}


/*
    Function which map our temporary positionId into global positionId, based on what kind building we have.
    type = 1 mean building
    type = 2 mean sculpture
*/
int idConversion(int position, int type) {
    int real_id = -1;
    if( type == 1) {
        switch(position) {
        case 1:
            real_id = 9;
            break;
        case 2:
            real_id = 4;
            break;
        case 3:
            real_id = 8;
            break;
        case 4:
            real_id = 6;
            break;
        case 5:
            real_id = 1;
            break;
        default:
            real_id = -1;
            break;
        }
    } else if(type == 2) {
        switch(position) {
        case 1:
            real_id = 7;
            break;
        case 2:
            real_id = 10;
            break;
        case 3:
            real_id = 3;
            break;
        case 4:
            real_id = 5;
            break;
        default:
            real_id = -1;
            break;
        }
    }
    return real_id;
}


/*
    Initialize parameters before predictions and project new feature into subspace
*/
Mat initializePrediction(Mat &featureMean, Mat &eigenvectors, Mat &feature) {
    buildingsDist.clear();
    sumArr.clear();
    for(int i = 0; i < 10; i++) {
        sumArr.push_back(0);
    }
    return LDA::subspaceProject(eigenvectors, featureMean, feature);
}

/*
    Sort distances and predict right building based on majority classifier
*/
int endPrediction() {
    int max_val = 0;
    int bestId = -1;
    sort(buildingsDist.begin(), buildingsDist.end(), cmd);
    for(int i = 0; i < N_EXAMPLES; i++) {
        sumArr[buildingsDist[i].pos - 1] +=1;
    }
    for(unsigned int i = 0; i < sumArr.size(); i++) {
        if(sumArr[i] > max_val) {
            bestId = i+1;
            max_val = sumArr[i];
        }
    }
    return bestId;
}

/*
    Function which is used for prediciting falsePositivies and of type of building.
*/
int predictBasic(Mat &feature, Mat &labels, Mat &featureMean, Mat &eigenvectors, Mat &features) {
    Mat projection = initializePrediction(featureMean, eigenvectors, feature);
    for (int i=0; i<features.rows; i++) {
        double d = norm( features.row(i), projection);
        pointDistance.dist = d;
        pointDistance.pos = labels.at<int>(i);
        buildingsDist.push_back(pointDistance);
    }
    if(buildingsDist.size() < N_EXAMPLES)
        return -1;
    return endPrediction();
}

/*
    Function which is used for prediciting buildings or sculpture (based on type).
*/
int predictBuild(Mat &feature, Mat &labels, Mat &featureMean, Mat &eigenvectors, Mat &features, int type, vector<picturePoint> buildingsAvailable) {
    Mat proj = initializePrediction(featureMean, eigenvectors, feature);
    for (int i=0; i<features.rows; i++) {
        double d = norm( features.row(i), proj);
        if ( d < 100) {
            int pos = idConversion(labels.at<int>(i), type);
            if(pos != -1) {
                for(unsigned int potenc = 0; potenc < buildingsAvailable.size(); potenc++) {
                    if(pos == buildingsAvailable[potenc].pos) {
                        pointDistance.dist = d;
                        pointDistance.pos = pos;
                        buildingsDist.push_back(pointDistance);
                        break;
                    }
                }
            }
        }
    }

    if(buildingsDist.size() < N_EXAMPLES)
        return -1;

    return endPrediction();
}